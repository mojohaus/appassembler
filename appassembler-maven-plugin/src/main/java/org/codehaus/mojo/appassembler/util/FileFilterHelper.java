package org.codehaus.mojo.appassembler.util;

/*
 * The MIT License
 *
 * Copyright (c) 2006-2012, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

/**
 * This is helper class to summarize all filters.
 *
 * @author <a href="mailto:codehaus@soebes.de">Karl Heinz Marbaise</a>
 */
public class FileFilterHelper
{
    private FileFilterHelper()
    {
        
    }
    
    /**
     * Make the given IOFileFilter aware of directories.
     *
     * @param filter The filter to make aware of directories.
     * @param directoryName The directory name which should be payed attention to.
     * @return The new generated filter.
     */
    public static IOFileFilter makeDirectoryAware( IOFileFilter filter, String directoryName )
    {

        IOFileFilter directoryAwareFilter =
            FileFilterUtils.notFileFilter( FileFilterUtils.andFileFilter( FileFilterUtils.directoryFileFilter(),
                                                                          FileFilterUtils.nameFileFilter( directoryName ) ) );

        return FileFilterUtils.andFileFilter( filter, directoryAwareFilter );
    }

    /**
     * Make the given IOFileFilter aware of files.
     *
     * @param filter The filter to make aware of files.
     * @param fileName The file name which should be payed attention to.
     * @return The new generated filter.
     */
    public static IOFileFilter makeFileNameAware( IOFileFilter filter, String fileName )
    {
        IOFileFilter directoryAwareFilter =
            FileFilterUtils.notFileFilter( FileFilterUtils.andFileFilter( FileFilterUtils.fileFileFilter(),
                                                                          FileFilterUtils.nameFileFilter( fileName ) ) );

        return FileFilterUtils.andFileFilter( filter, directoryAwareFilter );
    }

    /**
     * Make the given IOFileFilter aware of an suffix.
     *
     * @param filter The filter to make aware of an suffix.
     * @param suffixFileName The suffix name which should be payed attention to.
     * @return The new generated filter.
     */
    public static IOFileFilter makeSuffixAware( IOFileFilter filter, String suffixFileName )
    {
        IOFileFilter directoryAwareFilter =
            FileFilterUtils.notFileFilter( FileFilterUtils.andFileFilter( FileFilterUtils.fileFileFilter(),
                                                                          FileFilterUtils.suffixFileFilter( suffixFileName ) ) );

        return FileFilterUtils.andFileFilter( filter, directoryAwareFilter );
    }

    /**
     * Make the given IOFileFilter aware of the given pattern.
     *
     * @param filter The filter to make aware of the pattern.
     * @param pattern The pattern which should be payed attention to.
     * @return The new generated filter.
     */
    public static IOFileFilter makePatternFileNameAware( IOFileFilter filter, String pattern )
    {
        IOFileFilter directoryAwareFilter =
            FileFilterUtils.notFileFilter( FileFilterUtils.andFileFilter( FileFilterUtils.fileFileFilter(),
                                                                          new RegexFileFilter( pattern ) ) );

        return FileFilterUtils.andFileFilter( filter, directoryAwareFilter );
    }

    /**
     * This will create a FileFilter which is the same as in plexus-utils (DirectoryScanner.DEFAULTEXCLUDES).
     *
     * @return The initialized filter.
     */
    public static IOFileFilter createDefaultFilter()
    {

        IOFileFilter filter = null;

        // CVS
        // "**/CVS", "**/CVS/**",
        filter = FileFilterUtils.makeCVSAware( filter );

        // "**/.cvsignore",
        filter = makeFileNameAware( filter, ".cvsignore" );

        // Subversion
        // "**/.svn", "**/.svn/**",
        filter = FileFilterUtils.makeSVNAware( filter );

        // RCS
        // "**/RCS", "**/RCS/**",
        filter = makeDirectoryAware( filter, "RCS" );

        // SCCS
        // "**/SCCS", "**/SCCS/**",
        filter = makeDirectoryAware( filter, "SCCS" );

        // "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",
        filter = makeSuffixAware( filter, "~" );
        filter = makePatternFileNameAware( filter, "#.*#" );
        filter = makePatternFileNameAware( filter, "%.*%" );
        filter = makeSuffixAware( filter, ".#" );
        filter = makeSuffixAware( filter, "._" );

        // Visual SourceSafe
        // "**/vssver.scc",
        filter = makeFileNameAware( filter, "vssver.scc" );

        // MKS
        // "**/project.pj",
        filter = makeFileNameAware( filter, "project.pj" );

        // Arch
        // "**/.arch-ids", "**/.arch-ids/**",
        filter = makeDirectoryAware( filter, ".arch-ids" );

        // Bazaar
        // "**/.bzr", "**/.bzr/**",
        filter = makeDirectoryAware( filter, ".bzr" );

        // SurroundSCM
        // "**/.MySCMServerInfo",
        filter = makeFileNameAware( filter, ".MySCMServerInfo" );

        // Mac
        // "**/.DS_Store",
        filter = makeDirectoryAware( filter, ".DS_Store" );

        // Serena Dimensions Version 10
        // "**/.metadata", "**/.metadata/**",
        filter = makeDirectoryAware( filter, ".metadata" );

        // Mercurial
        // "**/.hg", "**/.hg/**",
        filter = makeDirectoryAware( filter, ".hg" );

        // git
        // "**/.git", "**/.gitignore", "**/.gitattributes", "**/.git/**",
        filter = makeDirectoryAware( filter, ".git" );
        filter = makeFileNameAware( filter, ".gitignore" );

        // BitKeeper
        // "**/BitKeeper", "**/BitKeeper/**", "**/ChangeSet", "**/ChangeSet/**",
        filter = makeDirectoryAware( filter, "BitKeeper" );
        filter = makeDirectoryAware( filter, "ChangeSet" );

        // darcs
        // "**/_darcs", "**/_darcs/**", "**/.darcsrepo", "**/.darcsrepo/**", "**/-darcs-backup*", "**/.darcs-temp-mail"
        // };
        filter = makeDirectoryAware( filter, "_darcs" );
        filter = makeDirectoryAware( filter, ".darcsrepo" );

        return filter;
    }

}
